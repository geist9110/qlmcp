import * as cdk from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { InfraStack } from "../lib/infra-stack";

describe("InfraStack", () => {
  let app: cdk.App;
  let stack: InfraStack;
  let template: Template;

  beforeEach(() => {
    app = new cdk.App();
    stack = new InfraStack(app, "test-stack", "test", {
      env: {
        account: "test-account",
        region: "us-east-1",
      },
    });
    template = Template.fromStack(stack);
  });
});
