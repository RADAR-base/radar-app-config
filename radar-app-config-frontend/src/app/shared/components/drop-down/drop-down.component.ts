import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatSelectChange} from "@angular/material/select";

/**
 * DropDown Component
 */
@Component({
  selector: 'app-dropdown',
  templateUrl: './drop-down.component.html',
  // styleUrls: ['./drop-down.component.scss']
})

export class DropDownComponent {
  _data: DropDownItem[] | null = null;

  /**
   * array of dropdown items
   */
  @Input() set data(value: DropDownItem[]) {
    if (!value) {
      this._data = null;
      return;
    }
    this._data = value.map(d => ({
      id: d.id || '',
      value: typeof d.value === 'string' ? d.value : (d.id || '')
    }))
  }

  get data(): DropDownItem[] {
    return this._data;
  }

  /**
   * selected item
   */
  @Input() selected: string;

  /**
   * dropdown label
   */
  @Input() label: string;

  /**
   * change event emitter
   */
  @Output() selectionUpdates: EventEmitter<string> = new EventEmitter<string>();

  /**
   * On dropdown item click, emit an event to parent
   * @param item: dropdown item
   */
  onChange(item: MatSelectChange) {
      this.selectionUpdates.emit(item.value);
  }
}

export interface DropDownItem {
  id: string;
  value?: string;
}
