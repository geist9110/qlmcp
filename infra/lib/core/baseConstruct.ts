import { Tags } from "aws-cdk-lib";
import { Construct } from "constructs";

export interface BaseConstructProps {
  project: string;
  envName: string;
}

export class BaseConstruct extends Construct {
  protected readonly project: string;
  protected readonly environment: string;

  constructor(scope: Construct, id: string, props: BaseConstructProps) {
    super(scope, id);

    this.project = props.project;
    this.environment = props.envName;

    this.addTags(id);
  }

  private addTags(id: string) {
    Tags.of(this).add("Project", this.project);
    Tags.of(this).add("Environment", this.environment);
    Tags.of(this).add("Name", `${this.project}-${this.environment}-${id}`);
  }
}
