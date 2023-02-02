export interface Config {
  clientId?: string;
  scope?: string;
  config?: ConfigElement[];
  defaults?: ConfigElement[];
}

export interface ConfigElement {
  name: string;
  value: string;
  default?: string;
  scope?: string;
}
