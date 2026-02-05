import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { ViewCaseComponent } from './view-case.component';

const routes: Routes = [{ path: '', component: ViewCaseComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ViewCaseRoutingModule { }
