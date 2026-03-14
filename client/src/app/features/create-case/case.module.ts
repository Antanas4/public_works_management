import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CaseRoutingModule } from './case-routing.module';
import { CaseFormTemplateComponent } from './components/case-form-template/case-form-template.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SharedModule} from "../../shared/shared.module";
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@NgModule({
  declarations: [CaseFormTemplateComponent],
  imports: [
    CommonModule,
    CaseRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ]
})
export class CaseModule { }
