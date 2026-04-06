import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardAdminRoutingModule } from './dashboard-admin-routing.module';
import { DashboardAdminComponent } from './dashboard-admin.component';
import { CaseListComponent } from '../../components/case-list/case-list.component';
import {SharedModule} from "../../../../shared/shared.module";
import {FormsModule} from "@angular/forms";

@NgModule({
  declarations: [DashboardAdminComponent, CaseListComponent],
    imports: [
        CommonModule,
        DashboardAdminRoutingModule,
        SharedModule,
        FormsModule,
    ]
})
export class DashboardAdminModule { }
