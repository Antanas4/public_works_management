import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { ViewCaseClientComponent } from './view-case-client.component';
import {ViewCaseAdminComponent} from "../view-case-admin/view-case-admin.component";

const routes: Routes = [{ path: '', component: ViewCaseClientComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ViewCaseClientRoutingModule { }
