import { SESClient, SendEmailCommand } from "@aws-sdk/client-ses";

const ses = new SESClient({ region: process.env.AWS_REGION });
const FROM_EMAIL = process.env.SES_FROM_EMAIL;

export const handler = async (event) => {
    for (const record of event.Records) {
        const snsEnvelope = JSON.parse(record.body);
        const payload = JSON.parse(snsEnvelope.Message);

        const { email, recoverUrl } = payload.data;

        await ses.send(new SendEmailCommand({
            Source: FROM_EMAIL,
            Destination: { ToAddresses: [email] },
            Message: {
                Subject: {
                    Data: "Password Recovery - Biblioteca",
                    Charset: "UTF-8"
                },
                Body: {
                    Html: {
                        Data: buildTemplate(recoverUrl),
                        Charset: "UTF-8"
                    }
                }
            }
        }));
    }
};

function buildTemplate(recoverUrl) {
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
                <h1 style="color:#fff;margin:0;font-size:22px;">Password Recovery</h1>
              </td>
            </tr>
            <tr>
              <td style="padding:40px 30px;">
                <p style="font-size:16px;color:#333;margin:0 0 16px;">
                  We received a request to reset your password.
                </p>
                <p style="font-size:15px;color:#555;line-height:1.6;margin:0 0 30px;">
                  Click the button below to set a new password.
                  This link is valid for 30 minutes.
                </p>
                <table cellpadding="0" cellspacing="0">
                  <tr>
                    <td style="background:#185FA5;border-radius:6px;padding:14px 28px;">
                      <a href="${recoverUrl}"
                         style="color:#fff;text-decoration:none;font-size:15px;font-weight:bold;">
                        Reset Password
                      </a>
                    </td>
                  </tr>
                </table>
                <p style="font-size:13px;color:#aaa;margin:24px 0 0;">
                  If you did not request a password reset, ignore this email.
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
