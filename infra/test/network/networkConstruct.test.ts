import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import { NetworkConstruct } from "../../lib/network/networkConstruct";
describe("network-construct-test", () => {
  let app: cdk.App;
  let stack: cdk.Stack;
  let template: Template;
  let construct: NetworkConstruct;

  beforeEach(() => {
    app = new cdk.App();
    stack = new cdk.Stack(app, "test-stack");

    construct = new NetworkConstruct(stack, "network-construct-test", {
      envName: "test",
      project: "qlmcp",
    });

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] CIDR block test", () => {
    template.hasResourceProperties(ec2.CfnVPC.CFN_RESOURCE_TYPE_NAME, {
      CidrBlock: Match.stringLikeRegexp("^10\\.0\\.0\\.0/16$"),
    });
  });

  test("[SUCCESS] NatGateway test", () => {
    template.resourceCountIs(ec2.CfnNatGateway.CFN_RESOURCE_TYPE_NAME, 0);
  });

  test("[SUCCESS] Subnet test", () => {
    template.hasResourceProperties(ec2.CfnSubnet.CFN_RESOURCE_TYPE_NAME, {
      MapPublicIpOnLaunch: true,
      CidrBlock: Match.stringLikeRegexp("^10\\.0\\.0\\.0/24$"),
    });

    template.resourceCountIs(ec2.CfnSubnet.CFN_RESOURCE_TYPE_NAME, 4);
  });

  test("[SUCCESS] Internet Gateway test", () => {
    template.resourceCountIs(ec2.CfnInternetGateway.CFN_RESOURCE_TYPE_NAME, 1);
  });
});
