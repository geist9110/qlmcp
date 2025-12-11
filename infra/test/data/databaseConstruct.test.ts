import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";
import { DatabaseConstruct } from "../../lib/data/databaseConstruct";

describe("database-construct-test", () => {
  let app: cdk.App;
  let stack: cdk.Stack;
  let template: Template;
  let construct: DatabaseConstruct;
  let mainServerSecurityGroup: ec2.SecurityGroup;
  let vpc: ec2.Vpc;

  beforeEach(() => {
    app = new cdk.App();
    stack = new cdk.Stack(app, "test-stack");

    vpc = new ec2.Vpc(stack, "test-vpc", {
      subnetConfiguration: [
        {
          name: "Public",
          subnetType: ec2.SubnetType.PUBLIC,
          cidrMask: 24,
        },
        {
          name: "Private",
          subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
          cidrMask: 24,
        },
      ],
    });

    mainServerSecurityGroup = new ec2.SecurityGroup(
      stack,
      "test-main-server-security-group",
      {
        vpc: vpc,
      },
    );

    construct = new DatabaseConstruct(stack, "database-construct-test", {
      envName: "test",
      project: "qlmcp",
      mainServerSecurityGroup: mainServerSecurityGroup,
      vpc: vpc,
    });

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] security group allows traffic only from main server sg", () => {
    const mainSgLogicalId = stack.getLogicalId(
      mainServerSecurityGroup.node.defaultChild as ec2.CfnSecurityGroup,
    );

    template.hasResourceProperties(
      ec2.CfnSecurityGroup.CFN_RESOURCE_TYPE_NAME,
      {
        SecurityGroupIngress: Match.arrayWith([
          Match.objectLike({
            IpProtocol: "tcp",
            FromPort: 3306,
            ToPort: 3306,
            SourceSecurityGroupId: Match.objectLike({
              "Fn::GetAtt": Match.arrayWith([mainSgLogicalId, "GroupId"]),
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

  test("[SUCCESS] database test", () => {
    const dbSgLogicalId = stack.getLogicalId(
      construct.securityGroup.node.defaultChild as ec2.CfnSecurityGroup,
    );

    template.hasResourceProperties(rds.CfnDBInstance.CFN_RESOURCE_TYPE_NAME, {
      DBInstanceClass:
        "db." +
        ec2.InstanceType.of(
          ec2.InstanceClass.T3,
          ec2.InstanceSize.MICRO,
        ).toString(),
      Engine: "mysql",
      EngineVersion: "8.0",
      DBName: "qlmcp",
      AllocatedStorage: "20",
      MaxAllocatedStorage: 100,
      PubliclyAccessible: false,
      VPCSecurityGroups: Match.arrayWith([
        { "Fn::GetAtt": [dbSgLogicalId, "GroupId"] },
      ]),
    });
  });

  test("[SUCCESS] database instance uses construct security group", () => {
    expect(construct.database.connections.securityGroups).toContain(
      construct.securityGroup,
    );
  });
});
