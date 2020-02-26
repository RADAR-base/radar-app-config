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
  private updateEnabled: boolean = false;

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
    this.updateEnabled = false;
  }

  onCancel() {
    const tempObject = {...this.route.snapshot.queryParams};
    delete tempObject.client;
    // TODO clients or global clients or users or groups?
    // console.log(this.global);
    if (this.global) {
      this.router.navigate(['/global-clients'], {queryParams: tempObject});
    } else {
      this.router.navigate(['/clients'], {queryParams: tempObject});
    }
  }
  
  // if change occurred save and reset activate
  // if change focus from input happens, check the change
  onBlur(event){
    const configValue = this.getConfigFormValue().config;
    // console.log(configValue);
    // if (this.updateEnabled === true) {return;}
    // console.log('onBlur',event);
    // console.log(this.configObject);
    // console.log(this.configForm.value.config);
    for (let i=0;i<configValue.length;i++){
      if(!this.configObject[i]) {this.updateEnabled = true; return;}
    //   // console.log(this.configObject[i].name != this.configForm.value.config[i].name);
    //   // console.log(this.configObject[i].value != this.configForm.value.config[i].value);
      if(this.configObject[i].name != configValue[i].name) {
        this.updateEnabled = true;
        return;
        // console.log('updateEnabled');
      }
      if(this.configObject[i].value != configValue[i].value) {
        this.updateEnabled = true;
        return;
        // console.log('updateEnabled');
      }
    }
    this.updateEnabled = false;
  }

  getConfigFormValue() {
    // console.log(this.configForm.value.config);
    let newConfigFormValue = [];
    for(let i=0;i<this.configForm.value.config.length;i++){
      if(this.configForm.value.config[i].name !== "" && this.configForm.value.config[i].value !== "" ){
        newConfigFormValue.push(this.configForm.value.config[i]);
      }
    }
    return {config: newConfigFormValue};

  }
}
