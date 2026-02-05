import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {CaseFormReactiveComponent} from "./components/case-form-reactive/case-form-reactive.component";


const routes: Routes = [
  {path: 'create', component: CaseFormReactiveComponent}

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CaseRoutingModule { }
