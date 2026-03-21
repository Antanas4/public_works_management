import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ViewCaseClientRoutingModule} from './view-case-client-routing.module';
import {ViewCaseClientComponent} from './view-case-client.component';
import {SharedModule} from "../../../../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {ViewCaseSharedModule} from "../../view-case-shared.module";
import {ProcessingHistoryComponent} from "../../components/processing-history/processing-history.component";


@NgModule({
  declarations: [ViewCaseClientComponent],
  imports: [
    CommonModule,
    ViewCaseClientRoutingModule,
    SharedModule,
    FormsModule,
    ViewCaseSharedModule,
    ProcessingHistoryComponent
  ]

})
export class ViewCaseClientModule {
  
}
