import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CreateCaseRoutingModule } from './create-case-routing.module';
import { CaseFormTemplateComponent } from './components/case-form-template/case-form-template.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SharedModule} from "../../shared/shared.module";
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

@NgModule({
  declarations: [CaseFormTemplateComponent],
  imports: [
    CommonModule,
    CreateCaseRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule
  ]
})
export class CreateCaseModule { }
