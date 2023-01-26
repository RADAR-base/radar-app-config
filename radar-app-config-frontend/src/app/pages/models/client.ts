import {DropDownItem} from "@app/shared/components/drop-down/drop-down.component";
import {User} from "@app/pages/models/user";

export interface Client {
  clientId: string;
}


export function clientsToDropDown(clients: Client[]): DropDownItem[] | null {
  if (!clients) return null;
  return clients.map(c => ({
    id: c.clientId,
  }))
}
