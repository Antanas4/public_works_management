import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CaseRoutingModule } from './case-routing.module';
import { CaseFormTemplateComponent } from './components/case-form-template/case-form-template.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SharedModule} from "../../shared/shared.module";
import { CaseFormReactiveComponent } from './components/case-form-reactive/case-form-reactive.component';


@NgModule({
  declarations: [CaseFormTemplateComponent, CaseFormReactiveComponent, CaseFormReactiveComponent],
    imports: [
        CommonModule,
        CaseRoutingModule,
        FormsModule,
        ReactiveFormsModule,
        SharedModule,
    ]
})
export class CaseModule { }
