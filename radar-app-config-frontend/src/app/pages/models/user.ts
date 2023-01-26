import {DropDownItem} from "@app/shared/components/drop-down/drop-down.component";

export class User {
  id: string;
  name: string;
  externalUserId?: string | null;
}

export function usersToDropDown(users: User[]): DropDownItem[] | null {
  if (!users) return null;
  return users.map(u => ({
    id: u.id,
    value: u.name,
  }))
}
