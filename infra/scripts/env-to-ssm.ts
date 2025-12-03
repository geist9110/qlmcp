import { PutParameterCommand, SSMClient } from "@aws-sdk/client-ssm";
import * as dotenv from "dotenv";
import * as path from "path";

const [, , modeArg] = process.argv;
type Mode = "dev" | "prod";
const mode: Mode = modeArg === "prod" ? "prod" : "dev";

// NOTE:
// The SSM parameter prefix is intentionally fixed to the production path (/qlmcp/prod).
//
// Reasoning:
// - The application only reads configuration from the production SSM namespace.
// - In local development, we mirror the same production namespace using LocalStack,
//   so all test writes also go to `/qlmcp/prod` but through the LocalStack endpoint.
// - Therefore, unlike typical multi-stage setups (dev/stage/prod), the SSM path
//   remains constant, and only the target endpoint (AWS vs LocalStack) changes.
//
// Important:
// - Be cautious when running in `prod` mode because it writes to the actual
//   AWS Parameter Store at `/qlmcp/prod`. Always double-check the env file.
const prefix = "/qlmcp/prod";
const filePath = path.join(__dirname, "..", "env", `.env.${mode}`);
const awsRegion = process.env.AWS_REGION ?? "us-east-1";
const ssm = new SSMClient(
  mode === "prod"
    ? { region: awsRegion }
    : {
        region: awsRegion,
        endpoint: "http://localhost:4566", // LocalStack url
        credentials: {
          accessKeyId: "test",
          secretAccessKey: "test",
        },
      },
);

async function main() {
  console.log("=== Environment To SSM Process ===");
  console.log(`Mode: ${mode}`);
  console.log(`File Path: ${filePath}`);
  console.log(`Region: ${awsRegion}`);

  const envMap = loadEnv(filePath);
  await sendToSSM(envMap);
}

function loadEnv(filePath: string): Record<string, string> {
  const env = dotenv.config({ path: filePath });
  if (env.error) {
    console.error("Failed to load env file:", env.error);
    process.exit(1);
  }
  return env.parsed ?? {};
}

async function sendToSSM(envMap: Record<string, string>): Promise<void> {
  for (const [key, value] of Object.entries(envMap)) {
    if (!value) {
      console.warn(`Skip ${key} (empty value)`);
      continue;
    }

    const name = `${prefix}/${key}`;
    const isSecret = /(PASSWORD|SECRET|API_KEY|TOKEN)/i.test(key);

    const command = new PutParameterCommand({
      Name: name,
      Value: value,
      Type: isSecret ? "SecureString" : "String",
      Overwrite: true,
    });

    console.log(
      `PutParameter: ${name} (type=${isSecret ? "SecureString" : "String"})`,
    );

    await ssm.send(command);
  }
}

if (require.main === module) {
  main().catch((error) => {
    console.log("Unexpected error: ", error);
    process.exit(1);
  });
}
