import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as dotenv from "dotenv";
import { InfraStack } from "../lib/infra-stack";

const env = "test";
dotenv.config({ path: `env/.env.${env}` });

describe("InfraStack", () => {
  let app: cdk.App;
  let stack: InfraStack;
  let template: Template;

  beforeEach(() => {
    app = new cdk.App();
    stack = new InfraStack(app, "TestStack", env, {
      env: {
        account: process.env.AWS_ACCOUNT_ID,
        region: process.env.AWS_REGION,
      },
    });
    template = Template.fromStack(stack);
  });

  describe("VPC 테스트", () => {
    test("하나의 VPC만 생성되는지 확인", () => {
      template.resourceCountIs("AWS::EC2::VPC", 1);
    });

    test("VPC가 /16 CIDR 블록을 가지는지 확인", () => {
      template.hasResourceProperties("AWS::EC2::VPC", {
        CidrBlock: Match.stringLikeRegexp("^10\\.0\\.0\\.0/16$"),
      });
    });

    test("VPC에 NatGateway가 없는지 확인", () => {
      template.resourceCountIs("AWS::EC2::NatGateway", 0);
    });

    test("VPC가 2*2개의 Subnet을 가지는지 확인", () => {
      template.resourceCountIs("AWS::EC2::Subnet", 4);
    });

    test("Subnet의 CIDR 마스크가 /24인지 확인", () => {
      template.hasResourceProperties("AWS::EC2::Subnet", {
        MapPublicIpOnLaunch: true,
        CidrBlock: Match.stringLikeRegexp("^10\\.0\\.0\\.0/24$"),
      });
    });

    test("Internet Gateway가 생성되는지 확인", () => {
      template.resourceCountIs("AWS::EC2::InternetGateway", 1);
    });

    test("VPC가 Internet Gateway에 연결되는지 확인", () => {
      template.hasResourceProperties(
        "AWS::EC2::Route",
        Match.objectLike({
          DestinationCidrBlock: "0.0.0.0/0",
          GatewayId: Match.anyValue(),
        }),
      );
    });
  });

  describe("Route53 테스트", () => {
    test("A 레코드가 메인 서버 인스턴스를 가리키는지 테스트", () => {
      template.hasResourceProperties("AWS::Route53::RecordSet", {
        Name: `mcp.qlmcp.com.`,
        Type: "A",
        ResourceRecords: Match.arrayWith([
          Match.objectLike({
            "Fn::GetAtt": Match.arrayWith([
              Match.stringLikeRegexp("qlmcpmainserver.*"),
              "PublicIp",
            ]),
          }),
        ]),
        TTL: "300",
      });
    });
  });
});
