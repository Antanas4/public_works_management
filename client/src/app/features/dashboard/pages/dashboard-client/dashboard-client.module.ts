import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DashboardClientRoutingModule} from "./dashboard-client-routing.module";
import {DashboardClientComponent} from "./dashboard-client.component";

@NgModule({
  declarations: [DashboardClientComponent],
  imports: [
    CommonModule,
    DashboardClientRoutingModule
  ]
})
export class DashboardClientModule { }
