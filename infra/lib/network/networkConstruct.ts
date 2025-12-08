import * as ec2 from "aws-cdk-lib/aws-ec2";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

export interface NetworkConstructProps extends BaseConstructProps {}

export class NetworkConstruct extends BaseConstruct {
  public readonly vpc: ec2.Vpc;

  constructor(scope: Construct, id: string, props: NetworkConstructProps) {
    super(scope, id, props);

    this.vpc = this.createVpc();
  }

  private createVpc(): ec2.Vpc {
    return new ec2.Vpc(this, "vpc", {
      maxAzs: 2,
      natGateways: 0,
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
  }
}
