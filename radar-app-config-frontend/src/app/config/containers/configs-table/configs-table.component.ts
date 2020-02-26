import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import strings from '@i18n/strings.json';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-configs-table',
  templateUrl: './configs-table.component.html',
  styleUrls: ['./configs-table.component.scss']
})
export class ConfigsTableComponent implements OnInit {
  private __ = strings;
  @Input() global;
  @Input() configObject;
  @Output() save = new EventEmitter();
  configForm: FormGroup;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private fb: FormBuilder) {}

  ngOnInit() {
    this.initialize();
  }

  getConfigs() {
    const control = this.configForm.get('config') as FormArray;
    for (const c of this.configObject) {
      const grp = this.fb.group({
        name: [c.name],
        value: [c.value],
        scope: [c.scope],
        default: [c.default]
      });
      control.push(grp);
    }
  }

  initialForm(): FormGroup {
    return this.fb.group({
      name: [''],
      value: [''],
      scope: [''],
      default: ['']
    });
  }

  get getFormData(): FormArray {
    return this.configForm.get('config') as FormArray;
  }

  addConfig() {
    const control = this.configForm.get('config') as FormArray;
    control.push(this.initialForm());
  }

  remove(index: number) {
    const control = this.configForm.get('config') as FormArray;
    control.removeAt(index);
  }

  onPublish() {
    this.save.emit(this.configForm.value);
  }

  initialize() {
    this.configForm = this.fb.group({
      config: this.fb.array([])
    });
    this.getConfigs();
  }

  onReset() {
    this.initialize();
  }

  onCancel() {
    const tempObject = {...this.route.snapshot.queryParams};
    delete tempObject.client;
    // TODO clients or global clients or users or groups?
    console.log(this.global);
    if (this.global) {
      this.router.navigate(['/global-clients'], {queryParams: tempObject});
    } else {
      this.router.navigate(['/clients'], {queryParams: tempObject});
    }
  }
}
