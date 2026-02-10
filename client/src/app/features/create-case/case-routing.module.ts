import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {CaseFormReactiveComponent} from "./components/case-form-reactive/case-form-reactive.component";
import {CaseFormTemplateComponent} from "./components/case-form-template/case-form-template.component";


const routes: Routes = [
  {path: 'create', component: CaseFormTemplateComponent}

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CaseRoutingModule { }
