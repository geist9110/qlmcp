import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as iam from "aws-cdk-lib/aws-iam";
import { McpServerConstruct } from "../../lib/compute/mcpServerConstruct";

describe("Mcp Server Test", () => {
  let app: cdk.App;
  let stack: cdk.Stack;
  let template: Template;
  let construct: McpServerConstruct;
  let mainServerSecurityGroup: ec2.SecurityGroup;

  beforeEach(() => {
    app = new cdk.App();
    stack = new cdk.Stack(app, "test-stack");

    const vpc = new ec2.Vpc(stack, "test-vpc", {
      maxAzs: 2,
      natGateways: 0,
    });

    mainServerSecurityGroup = new ec2.SecurityGroup(
      stack,
      "test-main-server-security-group",
      {
        vpc: vpc,
      },
    );

    construct = new McpServerConstruct(stack, "main-server", {
      project: "qlmcp",
      envName: "test",
      vpc,
      mainServerSecurityGroup: mainServerSecurityGroup,
    });

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] instance test", () => {
    template.hasResourceProperties(ec2.CfnInstance.CFN_RESOURCE_TYPE_NAME, {
      InstanceType: ec2.InstanceType.of(
        ec2.InstanceClass.T3,
        ec2.InstanceSize.MICRO,
      ).toString(),

      ImageId: Match.objectLike({
        Ref: Match.stringLikeRegexp(
          "SsmParameterValue.*amazonlinuxlatestal2023.*",
        ),
      }),

      SubnetId: Match.objectLike({
        Ref: Match.stringLikeRegexp(".*PublicSubnet.*"),
      }),
    });
  });

  test("[SUCCESS] security group test", () => {
    const mainConstructSecurityGroupLogicalId = stack.getLogicalId(
      mainServerSecurityGroup.node.defaultChild as ec2.CfnSecurityGroup,
    );

    template.hasResourceProperties(
      ec2.CfnSecurityGroup.CFN_RESOURCE_TYPE_NAME,
      {
        SecurityGroupIngress: Match.arrayWith([
          Match.objectLike({
            IpProtocol: "-1",
            SourceSecurityGroupId: Match.objectLike({
              "Fn::GetAtt": Match.arrayWith([
                mainConstructSecurityGroupLogicalId,
                "GroupId",
              ]),
            }),
          }),
        ]),
        SecurityGroupEgress: Match.arrayWith([
          Match.objectLike({
            IpProtocol: "-1",
            CidrIp: "0.0.0.0/0",
          }),
        ]),
      },
    );
  });

  test("[SUCCESS] role test", () => {
    template.hasResourceProperties(iam.CfnRole.CFN_RESOURCE_TYPE_NAME, {
      AssumeRolePolicyDocument: Match.objectLike({
        Statement: Match.arrayWith([
          Match.objectLike({
            Effect: "Allow",
            Principal: { Service: "ec2.amazonaws.com" },
            Action: "sts:AssumeRole",
          }),
        ]),
      }),
      ManagedPolicyArns: Match.arrayWith([
        Match.objectLike({
          "Fn::Join": Match.arrayWith([
            "",
            Match.arrayWith([
              "arn:",
              { Ref: "AWS::Partition" },
              ":iam::aws:policy/AmazonSSMManagedInstanceCore",
            ]),
          ]),
        }),
        Match.objectLike({
          "Fn::Join": Match.arrayWith([
            "",
            Match.arrayWith([
              "arn:",
              { Ref: "AWS::Partition" },
              ":iam::aws:policy/AmazonS3ReadOnlyAccess",
            ]),
          ]),
        }),
      ]),
    });
  });

  test("[SUCCESS] instance security group equals construct security group", () => {
    expect(construct.instance.connections.securityGroups).toContain(
      construct.securityGroup,
    );
  });

  test("[SUCCESS] instance role test", () => {
    expect(construct.instance.role).toEqual(construct.role);
  });
});
