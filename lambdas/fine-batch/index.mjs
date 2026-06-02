import { SESClient, SendEmailCommand } from "@aws-sdk/client-ses";
import { SecretsManagerClient, GetSecretValueCommand } from "@aws-sdk/client-secrets-manager";
import mysql from "mysql2/promise";

const ses = new SESClient({ region: process.env.AWS_REGION });
const secretsManager = new SecretsManagerClient({ region: process.env.AWS_REGION });

const FROM_EMAIL = process.env.SES_FROM_EMAIL;
const FINE_PER_DAY = parseFloat(process.env.FINE_PER_DAY || "1.00");
const DB_SECRET_ARN = process.env.DB_SECRET_ARN;

let dbConnection = null;

async function getDbCredentials() {
    const response = await secretsManager.send(
        new GetSecretValueCommand({ SecretId: DB_SECRET_ARN })
    );
    return JSON.parse(response.SecretString);
}

async function getConnection() {
    if (dbConnection && dbConnection.connection._closing === false) {
        return dbConnection;
    }

    const credentials = await getDbCredentials();

    dbConnection = await mysql.createConnection({
        host: credentials.host,
        port: parseInt(credentials.port),
        user: credentials.username,
        password: credentials.password,
        database: credentials.dbname
    });

    return dbConnection;
}

export const handler = async (event) => {
    console.log("Starting fine batch job at:", new Date().toISOString());

    const conn = await getConnection();
    const today = new Date().toISOString().split("T")[0];
    let processed = 0;
    let errors = 0;

    try {
        // Busca todos os loans OVERDUE com dados do usuario e livro
        const [loans] = await conn.execute(`
            SELECT
                l.id AS loanId,
                l.due_date AS dueDate,
                l.overdue_since AS overdueSince,
                l.fine_amount AS currentFine,
                l.overdue_notification_count AS notificationCount,
                u.id AS userId,
                u.name AS userName,
                u.email AS userEmail,
                b.title AS bookTitle
            FROM tb_loan l
            INNER JOIN tb_user u ON l.user_id = u.id
            INNER JOIN tb_book b ON l.book_id = b.id
            WHERE l.status = 'OVERDUE'
        `);

        console.log(`Found ${loans.length} overdue loans`);

        for (const loan of loans) {
            try {
                // Calcula dias de atraso a partir do overdue_since ou due_date
                const startDate = loan.overdueSince
                    ? new Date(loan.overdueSince)
                    : new Date(loan.dueDate);

                const todayDate = new Date(today);
                const daysOverdue = Math.floor(
                    (todayDate - startDate) / (1000 * 60 * 60 * 24)
                );

                // Calcula multa total acumulada
                const totalFine = (daysOverdue * FINE_PER_DAY).toFixed(2);

                // Atualiza fine_amount e fine_calculated_at no banco
                await conn.execute(`
                    UPDATE tb_loan
                    SET
                        fine_amount = ?,
                        fine_calculated_at = ?,
                        overdue_notification_count = overdue_notification_count + 1
                    WHERE id = ?
                `, [totalFine, today, loan.loanId]);

                // Envia email de cobrança
                await sendFineEmail(
                    loan.userEmail,
                    loan.userName,
                    loan.bookTitle,
                    daysOverdue,
                    totalFine,
                    loan.dueDate
                );

                processed++;
                console.log(`Processed loan ${loan.loanId} | user=${loan.userEmail} | fine=R$${totalFine}`);

            } catch (err) {
                errors++;
                console.error(`Error processing loan ${loan.loanId}:`, err.message);
            }
        }

    } finally {
        await conn.end();
        dbConnection = null;
    }

    const result = {
        date: today,
        processed,
        errors,
        finePerDay: FINE_PER_DAY
    };

    console.log("Fine batch completed:", result);
    return result;
};

async function sendFineEmail(email, name, bookTitle, daysOverdue, totalFine, dueDate) {
    await ses.send(new SendEmailCommand({
        Source: FROM_EMAIL,
        Destination: { ToAddresses: [email] },
        Message: {
            Subject: {
                Data: `Overdue Book Fine - Biblioteca`,
                Charset: "UTF-8"
            },
            Body: {
                Html: {
                    Data: buildTemplate(name, bookTitle, daysOverdue, totalFine, dueDate),
                    Charset: "UTF-8"
                }
            }
        }
    }));
}

function buildTemplate(name, bookTitle, daysOverdue, totalFine, dueDate) {
    return `
    <!DOCTYPE html>
    <html>
    <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0;">
      <table width="100%" cellpadding="0" cellspacing="0">
        <tr><td align="center" style="padding:20px;">
          <table width="600" cellpadding="0" cellspacing="0"
                 style="background:#fff;border-radius:8px;overflow:hidden;">

            <tr>
              <td style="background:#D85A30;padding:30px;text-align:center;">
                <h1 style="color:#fff;margin:0;font-size:22px;">
                  Overdue Book Notice
                </h1>
              </td>
            </tr>

            <tr>
              <td style="padding:40px 30px;">
                <p style="font-size:16px;color:#333;margin:0 0 16px;">
                  Hello, <strong>${name}</strong>!
                </p>
                <p style="font-size:15px;color:#555;line-height:1.6;margin:0 0 24px;">
                  You have an overdue book. Please return it as soon as possible
                  to avoid further charges.
                </p>

                <!-- Card do livro -->
                <table width="100%" cellpadding="0" cellspacing="0"
                       style="background:#fff5f2;border-radius:8px;
                              border-left:4px solid #D85A30;margin-bottom:24px;">
                  <tr>
                    <td style="padding:20px 24px;">
                      <p style="margin:0 0 8px;font-size:18px;
                                font-weight:bold;color:#D85A30;">
                        ${bookTitle}
                      </p>
                      <p style="margin:0;font-size:13px;color:#888;">
                        Due date: ${dueDate}
                      </p>
                    </td>
                  </tr>
                </table>

                <!-- Detalhes da multa -->
                <table width="100%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #eee;border-radius:8px;">
                  <tr style="background:#f9f9f9;">
                    <td style="padding:14px 20px;font-size:14px;color:#555;
                               border-bottom:1px solid #eee;">
                      Days overdue
                    </td>
                    <td style="padding:14px 20px;font-size:14px;color:#333;
                               font-weight:bold;text-align:right;
                               border-bottom:1px solid #eee;">
                      ${daysOverdue} day(s)
                    </td>
                  </tr>
                  <tr style="background:#f9f9f9;">
                    <td style="padding:14px 20px;font-size:14px;color:#555;
                               border-bottom:1px solid #eee;">
                      Fine per day
                    </td>
                    <td style="padding:14px 20px;font-size:14px;color:#333;
                               font-weight:bold;text-align:right;
                               border-bottom:1px solid #eee;">
                      R$ ${parseFloat(process.env.FINE_PER_DAY || 1).toFixed(2)}
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:16px 20px;font-size:16px;
                               color:#D85A30;font-weight:bold;">
                      Total fine
                    </td>
                    <td style="padding:16px 20px;font-size:18px;
                               color:#D85A30;font-weight:bold;text-align:right;">
                      R$ ${totalFine}
                    </td>
                  </tr>
                </table>

                <p style="font-size:13px;color:#aaa;
                          line-height:1.6;margin:24px 0 0;">
                  The fine increases by R$ ${parseFloat(process.env.FINE_PER_DAY || 1).toFixed(2)}
                  for each additional day the book is not returned.
                </p>
              </td>
            </tr>

            <tr>
              <td style="background:#f4f4f4;padding:20px;text-align:center;">
                <p style="color:#aaa;font-size:12px;margin:0;">
                  Biblioteca Digital — do not reply to this email
                </p>
              </td>
            </tr>

          </table>
        </td></tr>
      </table>
    </body>
    </html>`;
}
