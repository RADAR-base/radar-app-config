export class Config {
  clientId: string;
  scope: string;
  config: [{
    name: string;
    value: string;
    default: string;
  }];
  defaults: [
    {
      name: string;
      value: string;
      scope: string;
    }
  ];
}
