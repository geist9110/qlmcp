#!/usr/bin/env node
import { App } from "aws-cdk-lib";
import * as dotenv from "dotenv";
import { InfraStack } from "../lib/infra-stack";

const envName = process.env.NODE_ENV!;

if (!envName) {
  throw new Error("NODE_ENV is not defined");
}

dotenv.config({
  path: `env/.env.${process.env.NODE_ENV}`,
});

const app = new App();
new InfraStack(app, "InfraStack", envName, {
  env: {
    account: process.env.AWS_ACCOUNT_ID,
    region: process.env.AWS_REGION,
  },
});

