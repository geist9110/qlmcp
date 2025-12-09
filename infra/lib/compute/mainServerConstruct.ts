import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as iam from "aws-cdk-lib/aws-iam";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface MainServerConstructProps extends BaseConstructProps {
  vpc: ec2.Vpc;
}

export class MainServerConstruct extends BaseConstruct {
  public readonly instance: ec2.Instance;
  public readonly securityGroup: ec2.SecurityGroup;
  public readonly role: iam.Role;

  constructor(scope: Construct, id: string, props: MainServerConstructProps) {
    super(scope, id, props);

    this.securityGroup = this.createSecurityGroup(props);
    this.role = this.createRole();
    this.instance = this.createInstance(props);
  }

  private createSecurityGroup(
    props: MainServerConstructProps,
  ): ec2.SecurityGroup {
    const securityGroup = new ec2.SecurityGroup(this, "security-group", {
      vpc: props.vpc,
      allowAllOutbound: true,
      securityGroupName: `${props.project}-${props.envName}-main-server-security-group`,
    });

    securityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(443),
      "Allow HTTPS traffic from anywhere(IPv4)",
    );

    securityGroup.addIngressRule(
      ec2.Peer.anyIpv6(),
      ec2.Port.tcp(443),
      "Allow HTTPS traffic from anywhere(IPv6)",
    );

    securityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      "Allow HTTP traffic from anywhere(IPv4)",
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

  private createInstance(props: MainServerConstructProps): ec2.Instance {
    return new ec2.Instance(this, "instance", {
      vpc: props.vpc,
      instanceType: ec2.InstanceType.of(
        ec2.InstanceClass.T3,
        ec2.InstanceSize.SMALL,
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
