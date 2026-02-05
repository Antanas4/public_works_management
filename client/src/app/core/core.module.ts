import { NgModule} from "@angular/core";
import {HeaderComponent} from "./components/header/header.component";
import {CommonModule} from "@angular/common";
import {FooterComponent} from "./components/footer/footer.component";
import {I18nextModule} from "./i18next/i18next.module";
import {RouterModule} from "@angular/router";

@NgModule({
    declarations: [
        HeaderComponent,
        FooterComponent
    ],
    imports: [
        CommonModule,
        RouterModule,
        I18nextModule
    ],
    exports: [
        HeaderComponent,
        FooterComponent
    ],
})
export class CoreModule{}