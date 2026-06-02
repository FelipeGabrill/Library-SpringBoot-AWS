import { SESClient, SendEmailCommand } from "@aws-sdk/client-ses";

const ses = new SESClient({ region: process.env.AWS_REGION });
const FROM_EMAIL = process.env.SES_FROM_EMAIL;

export const handler = async (event) => {
    for (const record of event.Records) {
        const snsEnvelope = JSON.parse(record.body);
        const payload = JSON.parse(snsEnvelope.Message);

        const { name, email } = payload.data;

        await ses.send(new SendEmailCommand({
            Source: FROM_EMAIL,
            Destination: { ToAddresses: [email] },
            Message: {
                Subject: {
                    Data: "Welcome to Biblioteca!",
                    Charset: "UTF-8"
                },
                Body: {
                    Html: {
                        Data: buildTemplate(name),
                        Charset: "UTF-8"
                    }
                }
            }
        }));
    }
};

function buildTemplate(name) {
    return `
    <!DOCTYPE html>
    <html>
    <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0;">
      <table width="100%" cellpadding="0" cellspacing="0">
        <tr><td align="center" style="padding:20px;">
          <table width="600" cellpadding="0" cellspacing="0"
                 style="background:#fff;border-radius:8px;overflow:hidden;">
            <tr>
              <td style="background:#185FA5;padding:30px;text-align:center;">
                <h1 style="color:#fff;margin:0;font-size:22px;">Biblioteca Digital</h1>
              </td>
            </tr>
            <tr>
              <td style="padding:40px 30px;">
                <p style="font-size:16px;color:#333;margin:0 0 16px;">
                  Hello, <strong>${name}</strong>!
                </p>
                <p style="font-size:15px;color:#555;line-height:1.6;margin:0 0 30px;">
                  Your account has been created successfully.
                  Welcome to Biblioteca Digital!
                </p>
                <p style="font-size:14px;color:#555;line-height:1.6;margin:0;">
                  You can now search for books, borrow and reserve titles
                  from our collection.
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
