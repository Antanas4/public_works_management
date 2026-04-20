import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DashboardClientRoutingModule} from "./dashboard-client-routing.module";
import {DashboardClientComponent} from "./dashboard-client.component";
import {SharedModule} from "../../../../shared/shared.module";
import {MatButton} from "@angular/material/button";
import {FormsModule} from "@angular/forms";

@NgModule({
  declarations: [DashboardClientComponent],
  imports: [
    CommonModule,
    DashboardClientRoutingModule,
    SharedModule,
    MatButton,
    FormsModule
  ]
})
export class DashboardClientModule { }
