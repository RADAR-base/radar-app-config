import {Component, EventEmitter, Input, Output} from '@angular/core';

/**
 * DropDown Component
 */
@Component({
  selector: 'app-dropdown',
  templateUrl: './drop-down.component.html',
  // styleUrls: ['./drop-down.component.scss']
})

export class DropDownComponent{
  /**
   * array of dropdown items
   */
  @Input() data;

  /**
   * selected item
   */
  @Input() selected;

  /**
   * dropdown label
   */
  @Input() label;

  /**
   * field of item in data which should be displayed in dropdown (e.g. name or projectName)
   */
  @Input() field;

  /**
   * change event emitter
   */
  @Output() change: EventEmitter<string> = new EventEmitter<string>();

  /**
   * On dropdown item click, emit an event to parent
   * @param item: dropdown item
   */
  onChange(item: any) {
      this.change.emit(item);
  }
}
