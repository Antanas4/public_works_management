import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {CaseFormTemplateComponent} from "./components/case-form-template/case-form-template.component";
import {CaseTypeSelectComponent} from "../case-type-select/case-type-select.component";


const routes: Routes = [
  {path: 'create', component: CaseFormTemplateComponent},
  {path: 'select-case-type', component: CaseTypeSelectComponent}

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CreateCaseRoutingModule { }
