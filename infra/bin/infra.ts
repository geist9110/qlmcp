#!/usr/bin/env node
import {App} from 'aws-cdk-lib';
import {InfraStack} from '../lib/infra-stack';
import * as dotenv from 'dotenv';

const env = process.env.NODE_ENV!;

if (!env) {
  throw new Error('NODE_ENV is not defined');
}

dotenv.config({
  path: `env/.env.${process.env.NODE_ENV}`,
});

const domainName = process.env.DOMAIN_NAME!;
if (!domainName) {
  throw new Error('DOMAIN_NAME is not defined in environment variables');
}

const app = new App();
new InfraStack(app, 'InfraStack', domainName, env, {
  env: {
    account: process.env.AWS_ACCOUNT_ID,
    region: process.env.AWS_REGION,
  }
});