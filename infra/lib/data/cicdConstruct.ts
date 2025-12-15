import { RemovalPolicy, Stack } from "aws-cdk-lib";
import * as s3 from "aws-cdk-lib/aws-s3";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface CiCdConstructProps extends BaseConstructProps {}

export class CiCdConstruct extends BaseConstruct {
  public readonly buildArtifactStorage: s3.Bucket;

  constructor(scope: Construct, id: string, props: CiCdConstructProps) {
    super(scope, id, props);

    this.buildArtifactStorage = this.createBuildArtifactStorage(props);
  }

  private createBuildArtifactStorage(props: CiCdConstructProps): s3.Bucket {
    const removalPolicy = RemovalPolicy.DESTROY;
    const bucketName = `${props.project}-${props.envName}-build-artifact-${Stack.of(this).account}`;

    return new s3.Bucket(this, "build-artifact-storage", {
      bucketName: bucketName,
      encryption: s3.BucketEncryption.S3_MANAGED,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      objectOwnership: s3.ObjectOwnership.BUCKET_OWNER_ENFORCED,
      enforceSSL: true,
      removalPolicy: removalPolicy,
      autoDeleteObjects: removalPolicy === RemovalPolicy.DESTROY,
    });
  }
}
