import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {ViewCaseAdminRoutingModule} from './view-case-admin-routing.module';
import {ViewCaseAdminComponent} from './view-case-admin.component';
import {SharedModule} from '../../../shared/shared.module';
import {ViewCaseModule} from '../view-case-client/view-case.module';

import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {CommentsSectionComponent} from "../components/comments-section/comments-section.component";
import {ProcessingHistoryComponent} from "../components/processing-history/processing-history.component";


@NgModule({
  declarations: [ViewCaseAdminComponent],
  imports: [
    CommonModule,
    ViewCaseAdminRoutingModule,
    SharedModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    ViewCaseModule
  ]
})
export class ViewCaseAdminModule {
}
