import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ViewCaseAdminComponent} from "./view-case-admin.component";

const routes: Routes = [{ path: '', component: ViewCaseAdminComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ViewCaseAdminRoutingModule { }
