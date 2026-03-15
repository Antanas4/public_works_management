import { NgModule} from '@angular/core';
import {HeaderComponent} from './components/header/header.component';
import {CommonModule} from '@angular/common';
import {FooterComponent} from './components/footer/footer.component';
import {RouterModule} from '@angular/router';
import {MatButton} from "@angular/material/button";
import {MatToolbar} from "@angular/material/toolbar";

@NgModule({
    declarations: [
        HeaderComponent,
        FooterComponent
    ],
  imports: [
    CommonModule,
    RouterModule,
    MatButton,
    MatToolbar,
  ],
    exports: [
        HeaderComponent,
        FooterComponent
    ],
})
export class CoreModule {}