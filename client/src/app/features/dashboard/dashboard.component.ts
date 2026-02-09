import {Component} from '@angular/core';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss'],
    standalone: false
})
export class DashboardComponent {
  caseCount?: number;

  onTotalCasesChange(count: number) {
    this.caseCount = count;
  }
}
