import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ViewCaseRoutingModule } from './view-case-routing.module';
import { ViewCaseComponent } from './view-case.component';
import {SharedModule} from "../../shared/shared.module";
import { CommentsSectionComponent } from './components/comments-section/comments-section.component';
import { ProcessingHistoryComponent } from './components/processing-history/processing-history.component';
import {FormsModule} from "@angular/forms";


@NgModule({
  declarations: [ViewCaseComponent, CommentsSectionComponent, ProcessingHistoryComponent],
    imports: [
        CommonModule,
        ViewCaseRoutingModule,
        SharedModule,
        FormsModule,
    ]

})
export class ViewCaseModule { }
