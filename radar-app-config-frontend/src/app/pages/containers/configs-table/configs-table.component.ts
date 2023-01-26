import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, TemplateRef} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ModalDismissReasons, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {ToastService} from "@app/shared/services/toast.service";
import {Config, ConfigElement} from "@app/pages/models/config";

@Component({
  selector: 'app-configs-table',
  templateUrl: './configs-table.component.html',
  styleUrls: ['./configs-table.component.scss']
})
export class ConfigsTableComponent implements OnInit, OnChanges {

  @Input() globalConfig: boolean;
  @Input() configObject: ConfigElement[];
  @Output() save = new EventEmitter<Config>();
  configForm: FormGroup;
  updateEnabled = false;
  modalHeader = 'Modal Header';
  modalDescription: Modal;
  downloadJsonHref: SafeUrl;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private fb: FormBuilder,
              private modalService: NgbModal,
              private sanitizer: DomSanitizer,
              private toastService: ToastService,
  ) {}

  ngOnInit() {
    this.initialize();
    this.createExport();
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const k in changes) {
      if (changes.hasOwnProperty(k)) {
        this[k] = changes[k].currentValue;
      }
    }
    if (changes.hasOwnProperty('configObject')) {
      this.createExport();
      if (this.configForm) {
        (this.configForm.get('config') as FormArray).clear();
        this.createConfigForm();
      }
    }
  }

  createConfigForm() {
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
    control.at(index).patchValue({value: control.at(index).value.default});
    this.checkUpdateEnabled();
  }

  onPublish(content) {
    this.modalHeader = 'Publish configuration';
    this.modalDescription = {
      firstLine: `You are going to publish new configurations. Are you sure?`,
      secondLine: `All configuration will be overwritten permanently.`,
      thirdLine: `This operation cannot be undone.`
    };

    this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
      this.save.emit(this.configForm.value);
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
    });
  }

  initialize() {
    this.configForm = this.fb.group({
      config: this.fb.array([])
    });
    this.createConfigForm();
  }

  private createExport() {
    const configJson = JSON.stringify(this.configObject, null, 2);
    const blob = new Blob([configJson], { type: 'text/json' });
    const uri = URL.createObjectURL(blob);
    this.downloadJsonHref = this.sanitizer.bypassSecurityTrustUrl(uri);
  }

  onReset(content) {
    this.modalHeader = 'Reset configuration';
    this.modalDescription = {
      firstLine: `You are going to reset configurations. Are you sure?`,
      secondLine: `All configuration will be reset to previous one.`,
      thirdLine: `This operation cannot be undone.`
    };
    this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
      this.initialize();
      this.updateEnabled = false;
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
    });
  }

  onCancel(content) {
    this.modalHeader = 'Cancel configuration';
    this.modalDescription = {
      firstLine: `You are going to cancel configurations. Are you sure?`,
      secondLine: `All configuration will not be saved.`,
      thirdLine: `This operation cannot be undone.`
    };
    this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
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
    });
  }

  onImport(inputEvent, content: TemplateRef<any>) {
    const selectedFile: File = inputEvent.target.files[0];
    this.modalHeader = 'Import configuration';
    this.modalDescription = {
      firstLine: `You are going to publish new configurations from ${selectedFile.name}. Are you sure?`,
      secondLine: `All configuration will be overwritten permanently.`,
      thirdLine: `This operation cannot be undone.`
    };

    this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
      const reader = new FileReader();
      reader.onload = (event) => {
        const data: string = typeof event.target.result === 'string'
            ? event.target.result : JSON.stringify(event.target.result);
        try {
          const parsedData = JSON.parse(data);
          if (parsedData && Array.isArray(parsedData)) {
            this.save.emit({config: parsedData});
          } else {
            this.toastService.showError('Invalid data format');
          }
        } catch (e) {
          this.toastService.showError('Cannot parse JSON file');
        }
      };
      reader.readAsText(selectedFile);
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
    });
  }

  onChange(){
    this.checkUpdateEnabled();
  }

  checkUpdateEnabled() {
    const configValue = this.getConfigFormValue();
    this.updateEnabled = configValue.length !== this.configObject.length
        || !this.configObject.every((stored, i) => {
              const updated = configValue[i];
              return stored && updated && stored.name === updated.name && stored.value === updated.value;
        });
  }

  getConfigFormValue(): ConfigElement[] {
    return this.configForm.value.config.filter(c => c.name && c.value);
  }

  open(content) {
    this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      console.log(`Closed with: ${result}`);
    }, (reason) => {
      console.log(`Dismissed ${ConfigsTableComponent.getDismissReason(reason)}`);
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
