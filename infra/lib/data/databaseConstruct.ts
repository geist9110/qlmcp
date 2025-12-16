import { RemovalPolicy, SecretValue } from "aws-cdk-lib";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";
import * as ssm from "aws-cdk-lib/aws-ssm";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface DatabaseConstructProps extends BaseConstructProps {
  vpc: ec2.Vpc;
  mainServerSecurityGroup: ec2.SecurityGroup;
}

export class DatabaseConstruct extends BaseConstruct {
  public readonly database: rds.DatabaseInstance;
  public readonly securityGroup: ec2.SecurityGroup;

  constructor(scope: Construct, id: string, props: DatabaseConstructProps) {
    super(scope, id, props);

    this.securityGroup = this.createSecurityGroup(props);
    this.database = this.createDatabase(props);
  }

  private createSecurityGroup(
    props: DatabaseConstructProps,
  ): ec2.SecurityGroup {
    const securityGroup = new ec2.SecurityGroup(this, "security-group", {
      vpc: props.vpc,
      allowAllOutbound: true,
      securityGroupName: `${props.project}-${props.envName}-database-security-group`,
    });

    securityGroup.addIngressRule(
      ec2.Peer.securityGroupId(props.mainServerSecurityGroup.securityGroupId),
      ec2.Port.tcp(3306),
    );

    return securityGroup;
  }

  private createDatabase(props: DatabaseConstructProps): rds.DatabaseInstance {
    const databaseUserName = ssm.StringParameter.fromStringParameterName(
      this,
      "database-user-name",
      `/qlmcp/${props.envName}/infra/database/DB_USER`,
    ).stringValue;

    const databasePassword = SecretValue.ssmSecure(
      `/qlmcp/${props.envName}/infra/database/DB_PASSWORD`,
    );

    return new rds.DatabaseInstance(this, "database", {
      engine: rds.DatabaseInstanceEngine.mysql({
        version: rds.MysqlEngineVersion.VER_8_0,
      }),
      instanceType: ec2.InstanceType.of(
        ec2.InstanceClass.T3,
        ec2.InstanceSize.MICRO,
      ),
      vpc: props.vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
      },
      securityGroups: [this.securityGroup],
      multiAz: false,
      allocatedStorage: 20,
      maxAllocatedStorage: 100,
      databaseName: "qlmcp",
      credentials: rds.Credentials.fromPassword(
        databaseUserName,
        databasePassword,
      ),
      removalPolicy: RemovalPolicy.DESTROY,
    });
  }
}
