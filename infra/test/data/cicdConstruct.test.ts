import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as s3 from "aws-cdk-lib/aws-s3";
import { BaseConstructProps } from "../../lib/core/baseConstruct";
import { CiCdConstruct } from "../../lib/data/cicdConstruct";

describe("cicd-construct-test", () => {
  let app: cdk.App;
  let stack: cdk.Stack;
  let template: Template;
  const common: BaseConstructProps = {
    envName: "test",
    project: "qlmcp",
  };

  beforeEach(() => {
    app = new cdk.App();
    stack = new cdk.Stack(app, "test-stack");

    new CiCdConstruct(stack, "cicd-construct-test", common);

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] bucket has secure defaults and lifecycle rule", () => {
    template.hasResourceProperties(s3.CfnBucket.CFN_RESOURCE_TYPE_NAME, {
      BucketName: Match.objectLike({
        "Fn::Join": Match.arrayWith([
          "",
          Match.arrayWith([
            `${common.project}-${common.envName}-build-artifact-`,
            { Ref: "AWS::AccountId" },
          ]),
        ]),
      }),
      BucketEncryption: {
        ServerSideEncryptionConfiguration: Match.arrayWith([
          Match.objectLike({
            ServerSideEncryptionByDefault: {
              SSEAlgorithm: "AES256",
            },
          }),
        ]),
      },
      PublicAccessBlockConfiguration: {
        BlockPublicAcls: true,
        BlockPublicPolicy: true,
        IgnorePublicAcls: true,
        RestrictPublicBuckets: true,
      },
      OwnershipControls: {
        Rules: Match.arrayWith([
          Match.objectLike({ ObjectOwnership: "BucketOwnerEnforced" }),
        ]),
      },
    });
  });

  test("[SUCCESS] bucket policy enforces SSL", () => {
    template.hasResourceProperties(s3.CfnBucketPolicy.CFN_RESOURCE_TYPE_NAME, {
      PolicyDocument: {
        Statement: Match.arrayWith([
          Match.objectLike({
            Effect: "Deny",
            Principal: { AWS: "*" },
            Action: "s3:*",
            Resource: Match.arrayWith([
              {
                "Fn::GetAtt": Match.arrayWith([
                  Match.stringLikeRegexp(".*"),
                  "Arn",
                ]),
              },
              {
                "Fn::Join": Match.arrayWith([
                  "",
                  Match.arrayWith([{ "Fn::GetAtt": Match.anyValue() }, "/*"]),
                ]),
              },
            ]),
            Condition: {
              Bool: { "aws:SecureTransport": "false" },
            },
          }),
        ]),
      },
    });
  });

  test("[SUCCESS] auto delete custom resource is created for destroy policy", () => {
    template.resourceCountIs("Custom::S3AutoDeleteObjects", 1);
  });
});
