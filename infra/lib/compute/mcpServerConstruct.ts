import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as iam from "aws-cdk-lib/aws-iam";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface McpServerConstructProps extends BaseConstructProps {
  vpc: ec2.Vpc;
  mainServerSecurityGroup: ec2.SecurityGroup;
}

export class McpServerConstruct extends BaseConstruct {
  public readonly instance: ec2.Instance;
  public readonly role: iam.Role;
  public readonly securityGroup: ec2.SecurityGroup;

  constructor(scope: Construct, id: string, props: McpServerConstructProps) {
    super(scope, id, props);

    this.securityGroup = this.createSecurityGroup(props);
    this.role = this.createRole();
    this.instance = this.createInstance(props);
  }

  private createSecurityGroup(
    props: McpServerConstructProps,
  ): ec2.SecurityGroup {
    const securityGroup = new ec2.SecurityGroup(this, "security-group", {
      vpc: props.vpc,
      allowAllOutbound: true,
      securityGroupName: `${props.project}-${props.envName}-mcp-server-security-group`,
    });

    securityGroup.addIngressRule(
      ec2.Peer.securityGroupId(props.mainServerSecurityGroup.securityGroupId),
      ec2.Port.allTraffic(),
      "Allow all traffic from qlmcp main server",
    );

    return securityGroup;
  }

  private createRole(): iam.Role {
    return new iam.Role(this, "role", {
      assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com"),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName(
          "AmazonSSMManagedInstanceCore",
        ),
        iam.ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess"),
      ],
    });
  }

  private createInstance(props: McpServerConstructProps): ec2.Instance {
    return new ec2.Instance(this, "instance", {
      vpc: props.vpc,
      instanceType: ec2.InstanceType.of(
        ec2.InstanceClass.T3,
        ec2.InstanceSize.MICRO,
      ),
      machineImage: ec2.MachineImage.latestAmazonLinux2023(),
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      securityGroup: this.securityGroup,
      role: this.role,
    });
  }
}
