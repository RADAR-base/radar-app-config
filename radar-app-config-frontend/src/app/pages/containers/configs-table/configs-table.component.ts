import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ModalDismissReasons, NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-configs-table',
  templateUrl: './configs-table.component.html',
  styleUrls: ['./configs-table.component.scss']
})
export class ConfigsTableComponent implements OnInit {

  @Input() globalConfig;
  @Input() configObject;
  @Output() save = new EventEmitter();
  configForm: FormGroup;
  updateEnabled: boolean = false;
  modalHeader: any = "Modal Header";
  modalDescription: Modal;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private fb: FormBuilder,
              private _modalService: NgbModal) {}

  ngOnInit() {
    // console.log(this.configObject);
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
    this.checkUpdateEnabled();
  }

  backToDefault(index: number){
    const control = this.configForm.get('config') as FormArray;
    control.at(index).patchValue({"value": control.at(index).value.default});
    this.checkUpdateEnabled();
  }

  // onPublish() {
  //   this.save.emit(this.configForm.value);
  // }

  onPublish(content) {
    this.modalHeader = 'Publish configuration';
    this.modalDescription= {
      firstLine: `You are going to publish new configurations. Are you sure?`,
      secondLine: `All configuration will be overwritten permanently.`,
      thirdLine: `This operation cannot be undone.`
    };
    // this._modalService.open(NgbdModalConfirmAutofocus);
    // this.save.emit(this.configForm.value);

    this._modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
      this.save.emit(this.configForm.value);
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
      // this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  initialize() {
    this.configForm = this.fb.group({
      config: this.fb.array([])
    });
    this.getConfigs();
  }

  // onReset() {
  //   this.initialize();
  //   this.updateEnabled = false;
  // }

  onReset(content) {
    this.modalHeader = 'Reset configuration';
    this.modalDescription= {
      firstLine: `You are going to reset configurations. Are you sure?`,
      secondLine: `All configuration will be reset to previous one.`,
      thirdLine: `This operation cannot be undone.`
    };
    this._modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
      this.initialize();
      this.updateEnabled = false;
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
      // this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
    // this._modalService.open(NgbdModalConfirmAutofocus);
  }

  // onCancel() {
  //   const tempObject = {...this.route.snapshot.queryParams};
  //   delete tempObject.client;
  //   // TODO clients or global clients or users or groups?
  //   // console.log(this.global);
  //   if (this.global) {
  //     this.router.navigate(['/global-clients'], {queryParams: tempObject});
  //   } else {
  //     this.router.navigate(['/clients'], {queryParams: tempObject});
  //   }
  // }

  onCancel(content) {
    this.modalHeader = 'Cancel configuration';
    this.modalDescription= {
      firstLine: `You are going to cancel configurations. Are you sure?`,
      secondLine: `All configuration will not be saved.`,
      thirdLine: `This operation cannot be undone.`
    };
    this._modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
      const tempObject = {...this.route.snapshot.queryParams};
      delete tempObject.client;
      // TODO clients or global clients or users or groups?
      // console.log(this.global);
      if (this.globalConfig) {
        this.router.navigate(['/global-clients'], {queryParams: tempObject});
      } else {
        this.router.navigate(['/clients'], {queryParams: tempObject});
      }
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
      // this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
    // this._modalService.open(MODALS[name]);
  }
  
  // if change occurred save and reset activate
  // if change focus from input happens, check the change
  // onBlur(event){
  //   this.checkUpdateEnabled();
/*
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
    this.updateEnabled = false;*/
  // }

  onChange(event){
    this.checkUpdateEnabled();
  }

  checkUpdateEnabled(){
    const configValue = this.getConfigFormValue().config;
    for (let i=0;i<configValue.length;i++){
      if(!this.configObject[i]) {this.updateEnabled = true; return;}
      if(this.configObject[i].name != configValue[i].name) {
        this.updateEnabled = true;
        return;
      }
      if(this.configObject[i].value != configValue[i].value) {
        this.updateEnabled = true;
        return;
      }
    }
    if(configValue.length!==this.configObject.length){ this.updateEnabled = true; return;}
    this.updateEnabled = false;
  }


  getConfigFormValue() {
    let newConfigFormValue = [];
    for(let i=0;i<this.configForm.value.config.length;i++){
      if(this.configForm.value.config[i].name !== "" && this.configForm.value.config[i].value !== "" ){
        newConfigFormValue.push(this.configForm.value.config[i]);
      }
    }
    return {config: newConfigFormValue};

  }

  open(content) {
    this._modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
      // this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
      // this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  private static getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return  `with: ${reason}`;
    }
  }


}

interface Modal {
  firstLine: string;
  secondLine: string;
  thirdLine: string;
}
