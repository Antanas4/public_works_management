import {NgModule, OnInit} from "@angular/core";
import {ViewCaseAdminComponent} from "./view-case-admin.component";
import {MatCardModule} from "@angular/material/card";
import {MatSelectModule} from "@angular/material/select";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {RouterLink} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {SharedModule} from "../../../../shared/shared.module";
import {DatePipe} from "@angular/common";
import {ViewCaseAdminRoutingModule} from "./view-case-admin-routing.module";
import {ViewCaseSharedModule} from "../../view-case-shared.module";
import {ProcessingHistoryComponent} from "../../components/processing-history/processing-history.component";
import {MatListOption, MatSelectionList} from "@angular/material/list";
import {MatChip} from "@angular/material/chips";
import {MatProgressBar} from "@angular/material/progress-bar";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";

@NgModule({
  declarations: [ViewCaseAdminComponent],
  imports: [
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    ViewCaseSharedModule,
    RouterLink,
    FormsModule,
    SharedModule,
    DatePipe,
    ViewCaseAdminRoutingModule,
    ProcessingHistoryComponent,
    MatSelectionList,
    MatListOption,
    MatChip,
    MatProgressBar,
    MatRadioButton,
    MatRadioGroup
  ]

})
export class ViewCaseAdminModule implements OnInit{
  ngOnInit() {
    console.log(ViewCaseSharedModule);
  }
}