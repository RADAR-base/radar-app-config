import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-dropdown',
  templateUrl: './drop-down.component.html',
  styleUrls: ['./drop-down.component.scss']
})

export class DropDownComponent{
  @Input() data;
  @Input() selected;
  @Input() label;
  @Input() field;
  // tslint:disable-next-line:no-output-native
  @Output() change: EventEmitter<string> = new EventEmitter<string>();
  head;
  headIsActive: boolean;

  constructor(){}

  onChange(item: any) {
      this.change.emit(item);
  }
}
