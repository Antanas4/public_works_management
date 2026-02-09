import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardComponent } from './dashboard.component';
import { CaseListComponent } from './components/case-list/case-list.component';
import {SharedModule} from "../../shared/shared.module";
import {FormsModule} from "@angular/forms";

@NgModule({
  declarations: [DashboardComponent, CaseListComponent],
    imports: [
        CommonModule,
        DashboardRoutingModule,
        SharedModule,
        FormsModule,
    ]
})
export class DashboardModule { }
