/*
 * Copyright 2019 Project-Herophilus
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.connectedhealth_idaas.parsers;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@CsvRecord(separator = "\\|")
public class ReportingOutput {
    @DataField(pos = 1)
    private String organizationId;

    @DataField(pos = 2)
    private String patientAccount;

    @DataField(pos = 3)
    private String patientLastName;

    @DataField(pos = 4)
    private String patientFirstName;

    @DataField(pos = 5)
    private String zipCode;

    @DataField(pos = 6)
    private String roomBed;

    @DataField(pos = 7)
    private int age;

    @DataField(pos = 8)
    private String gender;

    //@DataField(pos = 8, pattern="yyyy-MM-dd")
    //@JsonFormat(pattern = "yyyy-MM-dd")
    //private Date admissionDate;
    @DataField(pos = 9)
    private String admissionDate;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getPatientAccount() {
        return patientAccount;
    }

    public void setPatientAccount(String patientAccount) {
        this.patientAccount = patientAccount;
    }

    public String getPatientLastName() {
        return patientLastName;
    }

    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
    }

    public String getPatientFirstName() {
        return patientFirstName;
    }

    public void setPatientFirstName(String patientFirstName) {
        this.patientFirstName = patientFirstName;
    }

    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getRoomBed() {
        return roomBed;
    }
    public void setRoomBed(String roomBed) {
        this.roomBed = roomBed;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

   /* public Date getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(Date admissionDate) {
        this.admissionDate = admissionDate;
    }
    */
    public String getAdmissionDate() {
        return admissionDate;
    }
    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
