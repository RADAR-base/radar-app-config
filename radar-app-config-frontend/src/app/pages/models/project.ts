import {DropDownItem} from "@app/shared/components/drop-down/drop-down.component";
import {Client} from "@app/pages/models/client";

export interface Project {
  projectName: string;
  humanReadableProjectName?: string;
  location: string;
  organization: string;
  description: string;
}


export function projectsToDropDown(projects?: Project[]): DropDownItem[] | null {
  if (!projects) return null;
  return projects.map(p => ({
    id: p.projectName,
  }))
}
