import {Component} from '@angular/core';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard-admin.component.html',
    styleUrls: ['./dashboard-admin.component.scss'],
    standalone: false
})
export class DashboardAdminComponent {
  caseCount?: number;

  onTotalCasesChange(count: number) {
    this.caseCount = count;
  }
}
