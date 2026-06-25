import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { InboxItem } from "../models/inbox.model";


@Injectable({ providedIn:'root'})
export class InboxService {
    private http = inject(HttpClient);
    private url = `${environment.apiUrl}/inbox`;

    capture(data: FormData){
        return this.http.post(this.url,data);
    }

    list(){
        return this.http.get<{data : InboxItem[]}>(this.url);
    }

    download(id:string){
        return this.http.get(`${this.url}/${id}/download`, {responseType: 'arraybuffer'});
    }

    delete(id:string){
        return this.http.delete(`${this.url}/${id}`);
    }

    markStaged(id:string, type:string, targetId:string){
        return this.http.put(`${this.url}/${id}/stage?type=${type}&targetId=${targetId}`,{});
    }

}