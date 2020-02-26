import {Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {ToastService} from '@app/shared/services/toast.service';

@Component({
  selector: 'app-dropdown',
  templateUrl: './drop-down.component.html',
  styleUrls: ['./drop-down.component.scss']
})

export class DropDownComponent implements OnInit {
  @Input() data;
  @Input() selected;
  // tslint:disable-next-line:no-output-native
  @Output() change: EventEmitter<string> = new EventEmitter<string>();
  private head;
  private headIsActive: boolean;

  constructor(
    private eRef: ElementRef,
    private toastService: ToastService) {}

  ngOnInit() {
    this.head = this.data.find(d => d.name == this.selected ); // TODO
    if (!this.head) { this.toastService.showError('Id error'); } // TODO
  }

  onHeadClick() {
    this.headIsActive = !this.headIsActive;
  }

  onItemClick(item) {
    this.headIsActive = !this.headIsActive;
    this.head = item;
    this.change.emit(item);
  }

  @HostListener('document:click', ['$event'])
  clickout(event) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.headIsActive = false;
    }
  }
}
