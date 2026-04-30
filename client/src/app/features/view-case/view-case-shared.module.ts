import {ProcessingHistoryComponent} from "./components/processing-history/processing-history.component";
import {CommentsSectionComponent} from "./components/comments-section/comments-section.component";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {SharedModule} from "../../shared/shared.module";
import {NgModule} from "@angular/core";
import {MatButton} from "@angular/material/button";

@NgModule({
  declarations: [
    CommentsSectionComponent,
    ProcessingHistoryComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    SharedModule,
    MatButton
  ],
  exports: [
    CommentsSectionComponent,
    ProcessingHistoryComponent
  ]
})
export class ViewCaseSharedModule {}