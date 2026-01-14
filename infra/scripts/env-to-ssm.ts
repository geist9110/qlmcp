import { PutParameterCommand, SSMClient } from "@aws-sdk/client-ssm";
import * as dotenv from "dotenv";
import { promises as fs } from "fs";
import * as path from "path";

const [, , modeArg] = process.argv;
type Environment = "dev" | "prod";
const environment: Environment = modeArg === "prod" ? "prod" : "dev";

const environmentRoot: string = path.join(__dirname, "..", "env", environment);
const awsRegion = process.env.AWS_REGION ?? "us-east-1";
const ssm = new SSMClient(
  environment === "prod"
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
  console.log(`Environment: ${environment}`);
  console.log(`Region: ${awsRegion}`);

  const envFiles = await findEnvFiles(environmentRoot);
  console.log(
    `Found env files:`,
    envFiles.map((f) => path.relative(environmentRoot, f)),
  );
  await sendToSSM(envFiles);
}

async function findEnvFiles(dir: string): Promise<string[]> {
  const entries = await fs.readdir(dir, { withFileTypes: true });
  const files: string[] = [];
  for (const entry of entries) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...(await findEnvFiles(full)));
    } else if (entry.isFile() && entry.name === ".env") {
      files.push(full);
    }
  }

  return files;
}

async function sendToSSM(envFiles: string[]): Promise<void> {
  for (const filePath of envFiles) {
    const envMap = loadEnv(filePath);
    const relDir = path
      .dirname(path.relative(environmentRoot, filePath))
      .replace(/^\.\$/, "");
    const dirPrefix = relDir ? `/${relDir}` : "";

    for (const [key, value] of Object.entries(envMap)) {
      if (!value) {
        console.warn(`Skip ${key} (empty value)`);
        continue;
      }

      const name = `/qlmcp/${environment}${dirPrefix}/${key}`;
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
}

function loadEnv(filePath: string): Record<string, string> {
  const env = dotenv.config({ path: filePath });
  if (env.error) {
    console.error("Failed to load env file:", env.error);
    process.exit(1);
  }
  return env.parsed ?? {};
}

if (require.main === module) {
  main().catch((error) => {
    console.log("Unexpected error: ", error);
    process.exit(1);
  });
}
